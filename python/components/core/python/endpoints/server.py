from __main__ import app

import requests
import logging
import os

backend_host = os.getenv('BACKEND_HOST')
backend_port = os.getenv('BACKEND_PORT')

server_url = f'http://{backend_host}:{backend_port}/servers/v1'

@app.route('/servers/v1/<serverId>/info')
def get_server_info(serverId):
    server = requests.get(f'{server_url}/{serverId}/info')
    logging.info(f'returning server info')
    return server.text, server.status_code
