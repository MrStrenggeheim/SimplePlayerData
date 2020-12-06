#!/usr/bin/env python
import os
import threading
import argparse
from time import time
from flask import Flask
from flask import request, jsonify


last_interactions = dict()
player_list = "[ ]"
cape_list = set()

app = Flask(__name__)
app.config.from_object('simpleplayerdata.config.Config')
# app.config.from_object('config.Config')  # app.config.from_object('simpleplayerdata.config.Config') when in development with Flask

@app.before_first_request
def refresh_list():
    global last_interactions, player_list
    min_time = time() - app.config['TIMEOUT']
    last_interactions = {player: last_ping for player, last_ping in last_interactions.items() if last_ping >= min_time}
    with app.app_context():
        player_list = jsonify(list(last_interactions.keys()))
    threading.Timer(app.config['REFRESH'], refresh_list).start()


@app.before_first_request
def refresh_capes():
    global cape_list
    cape_list.clear()
    extension = '.' + str(app.config['CAPE_FILETYPE']).lower()
    for file in os.listdir(app.config['CAPE_DIR']):
        if file.endswith(extension):
            cape_list.add(file.replace(extension, ''))
    threading.Timer(app.config['CAPE_REFRESH'], refresh_capes).start()


@app.route('/', methods=['GET'])
def home():
    return "<h1>Player Data API</h1><p>Nothing to see here.</p>"


@app.route('/playerlist/fetch', methods=['POST'])
def get_player_list():
    if 'application/json' not in request.content_type:
        return 'Error: Invalid format'
    if 'names' in request.json:
        names = request.json['names']
        if not isinstance(names, list):
            return "Error: Invalid Request"
        if len(names) > 500:
            return "Error: Too many names"
        return jsonify([name for name in names if name in last_interactions])
    return player_list


@app.route('/playerlist/update', methods=['POST'])
def update_playerlist():
    # TODO: add spam limit and limit the number of player names an ip can register
    if 'name' not in request.values:
        return 'Error: No name provided'
    # Add the player to our player list
    if len(request.values['name']) > 16:
        return "Error: Invalid player name"
    last_interactions[request.values['name']] = time()
    return "Success!"


@app.route('/capes/fetch', methods=['POST'])
def get_cape_list():
    if 'application/json' not in request.content_type:
        return 'Error: Invalid format'
    capes = dict()
    if 'uuids' in request.json:
        base_url = app.config['CAPE_URL']
        extension = '.' + str(app.config['CAPE_FILETYPE']).lower()
        uuids = request.json['uuids']
        if not isinstance(uuids, list):
            return "Error: Invalid Request"
        if len(uuids) > 500:
            return "Error: Too many uuids"
        for uuid in uuids:
            if isinstance(uuid, str) and uuid in cape_list and len(uuid) < 64:
                capes[uuid] = base_url + uuid + extension
    return jsonify(capes)


@app.errorhandler(404)
def page_not_found(e):
    return "<h1>404</h1><p>The resource could not be found.</p>", 404


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='host a little web server that keeps a list of clients connected to the server')
    parser.add_argument('--timeout', dest='timeout', metavar='-t', type=int, default=180,
                        help='the time in seconds until a client should be considered disconnected')
    parser.add_argument('--refresh', dest='refresh', metavar='-r', type=int, default=30,
                        help='the interval in seconds in which the player list should be updated')
    args = parser.parse_args()

    app.config['timeout'] = args.timeout
    app.config['refresh'] = args.refresh
    app.run()
