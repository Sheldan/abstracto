from __main__ import app

from flask import request, render_template
import requests
import logging
import os

backend_host = os.getenv('BACKEND_HOST')
backend_port = os.getenv('BACKEND_PORT')

leaderboard_url = f'http://{backend_host}:{backend_port}/experience/v1/leaderboards'

@app.route('/experience/v1/leaderboards/<serverId>')
def get_leaderboard(serverId):
    page = int(request.args.get('page', 0, type=int))
    size = int(request.args.get('size', 25, type=int))
    leaderboard = requests.get(f'{leaderboard_url}/{serverId}?page={page}&size={size}')
    logging.info(f'returning leaderboard for server')
    return leaderboard.text, leaderboard.status_code

@app.route('/experience/v1/leaderboards/<serverId>/config')
def get_experience_config(serverId):
    leaderboard = requests.get(f'{leaderboard_url}/{serverId}/config')
    logging.info(f'returning experience config for server')
    return leaderboard.text, leaderboard.status_code


@app.route('/experience/leaderboards/<serverId>')
def render_index(serverId):
    return render_template('experience/leaderboards/index.html', serverId=serverId)