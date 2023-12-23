from __main__ import app

from PIL import Image, ImageOps
from flask import request
import requests
import validators
import logging
import io

from utils import flask_utils


allowed_content_length = 4_000_000
allowed_image_formats = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif']
max_off_set = 5
gif_speedup_factor = 4


@app.route('/memes/triggered/file.gif')  # to directly embed, discord _requires_ this file ending, it seems
def triggered_animated():
    url = request.args.get('url')
    if not validators.url(url):
        return 'no valid url', 400
    session = requests.Session()
    response = session.head(url)
    content_type = response.headers['content-type']

    if content_type not in allowed_image_formats:
        return f'Incorrect image type {content_type}', 400

    actual_content_length = int(response.headers['content-length'])
    if actual_content_length > allowed_content_length:
        return f'Image too large {actual_content_length}', 400

    image_file = requests.get(url, stream=True)
    input_image = Image.open(io.BytesIO(image_file.content))
    original_input_image = input_image
    old_width, old_height = input_image.size
    with Image.open('resources/img/triggered_footer.jpg') as footer_image:
        footer_width, footer_height = footer_image.size
        footer_ratio = old_width / footer_width
        desired_new_footer_width = int(footer_width * footer_ratio)
        if footer_ratio > 1:
            footer_image = footer_image.resize((desired_new_footer_width, footer_height))
        else:
            footer_image = ImageOps.contain(footer_image, (desired_new_footer_width, footer_height))
        new_footer_width, new_footer_height = footer_image.size
        new_total_height = old_height + new_footer_height
        points = [[0, -max_off_set], [max_off_set, 0], [0, max_off_set], [-max_off_set, 0]]
        if content_type == 'image/gif':
            logging.info(f'Rendering triggered for gif.')
            frame_count = original_input_image.n_frames
            old_frames = []
            for frame_index in range(frame_count):
                input_image.seek(frame_index)
                frame = input_image.convert('RGBA')
                old_frames.append(frame)
            frames = []
            for index, old_frame in enumerate(old_frames):
                off_set = points[index % len(points)]
                frame = Image.new('RGBA', (old_width, new_total_height), (0, 0, 0, 0))
                old_frame = ImageOps.contain(old_frame, (old_width + max_off_set * 2, old_height + max_off_set * 2))
                frame.paste(old_frame, (-max_off_set + off_set[0], -max_off_set + off_set[1]))
                frame.paste(footer_image, (0, old_height))
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames, (int(original_input_image.info['duration']) / gif_speedup_factor))
        else:
            input_image = ImageOps.contain(input_image, (old_width + max_off_set * 2, old_height + max_off_set * 2))
            frames = []
            logging.info(f'Rendering triggered for static image.')
            for off_set in points:
                frame = Image.new('RGBA', (old_width, new_total_height), (0, 0, 0, 0))
                frame.paste(input_image, (-max_off_set + off_set[0], -max_off_set + off_set[1]))
                frame.paste(footer_image, (0, old_height))
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames)
