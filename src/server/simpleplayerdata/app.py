import threading
import argparse
from time import time
from flask import Flask
from flask import request, jsonify


last_interactions = dict()
player_list = "[ ]"
debugger = 0

app = Flask(__name__)
app.config.from_object('simpleplayerdata.config.Config')


@app.before_first_request
def refresh_list():
    global last_interactions, player_list, debugger
    debugger = len(last_interactions)
    min_time = time() - app.config['TIMEOUT']
    last_interactions = {player: last_ping for player, last_ping in last_interactions.items() if last_ping >= min_time}
    with app.app_context():
        player_list = jsonify(list(last_interactions.keys()))
    threading.Timer(app.config['REFRESH'], refresh_list).start()


@app.route('/', methods=['GET'])
def home():
    return "<h1>Player Data API</h1><p>Nothing to see here.</p>"


@app.route('/api/players', methods=['GET'])
def get_player_list():
    # TODO: add spam limit and limit the number of player names an ip can register
    if 'player' in request.args:
        # Add the player to our player list
        if len(request.args['player']) > 16:
            return "Error: Invalid player name"
        last_interactions[request.args['player']] = time()
    return player_list


@app.errorhandler(404)
def page_not_found(e):
    return "<h1>404</h1><p>The resource could not be found.</p>", 404


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='host a little web server that keeps a list of clients connected to the server')
    parser.add_argument('--port', dest='port', metavar='-p', default=5000, type=int, help='the port number')
    parser.add_argument('--timeout', dest='timeout', metavar='-t', type=int, default=100,
                        help='the time in seconds until a client should be considered disconnected')
    parser.add_argument('--refresh', dest='refresh', metavar='-r', type=int, default=1,
                        help='the interval in seconds in which the player list should be updated')
    args = parser.parse_args()

    app.config['timeout'] = args.timeout
    app.config['refresh'] = args.refresh
    app.run(port=args.port)
