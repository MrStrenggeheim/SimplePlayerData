import requests
import json
import time


url = 'http://127.0.0.1:5000/api/players'


def fetch_list(player_name):
    response = requests.get(url, params={'player': player_name})
    return response.content


fetch_list("Test")
fetch_list("Test2")
time.sleep(31)
content = json.loads(fetch_list("Test3"))
print(content)
