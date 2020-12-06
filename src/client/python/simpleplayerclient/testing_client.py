import requests
import json
import time


url = 'http://127.0.0.1:5000/'


def fetch_list(player_name):
    print(requests.post(url + 'playerlist/update', json={'name': player_name}))
    response = requests.post(url + 'playerlist/fetch', json={'names': ['Test', 'Test2', 'Test3']})
    return response.content


fetch_list("Test")
content = json.loads(fetch_list("Test2"))
print(content)
time.sleep(31)
content = json.loads(fetch_list("Test3"))
print(content)
