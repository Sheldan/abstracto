from __main__ import app

from PIL import Image, ImageOps
from flask import request
import requests
import validators
import logging
import io

from utils import flask_utils


bonk_angles = [40, 45, -5, 0, 5, 45, -40, 45, -30, -5, 50, 40, 5, 0, -45, 15, 5, 40, 0, -45, 5, -40, 60, -50, -40]
allowed_content_length = 4_000_000
allowed_image_formats = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif']
gif_speedup_factor = 2


def get_avatar_height_factor(angle):
    return max(0.1, angle * -1 / 45)

@app.route('/memes/bonk/file.gif')  # to directly embed, discord _requires_ this file ending, it seems
def bonk_animated():
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
    with Image.open('resources/img/newspaper.png') as newspaper_image:
        newspaper_image = newspaper_image.convert('RGBA')
        newspaper_width, newspaper_height = newspaper_image.size
        newspaper_ratio = old_width / newspaper_width
        desired_new_newspaper_width = int(newspaper_width * newspaper_ratio)
        desired_newspaper_height = newspaper_height
        if newspaper_ratio > 1:
            newspaper_image = newspaper_image.resize((desired_new_newspaper_width, desired_newspaper_height))
        else:
            newspaper_image = ImageOps.contain(newspaper_image, (desired_new_newspaper_width, desired_newspaper_height))
        new_newspaper_width, new_newspaper_height = newspaper_image.size
        new_total_height = new_newspaper_height
        if content_type == 'image/gif':
            logging.info(f'Rendering bonk for gif.')
            frame_count = original_input_image.n_frames
            old_frames = []
            for frame_index in range(frame_count):
                input_image.seek(frame_index)
                frame = input_image.convert('RGBA')
                old_frames.append(frame)
            frames = []
            current_factor = 1
            for index, old_frame in enumerate(old_frames):
                angle = bonk_angles[index % len(bonk_angles)]
                frame = Image.new('RGBA', (old_width, new_total_height), (0, 0, 0, 0))
                current_factor *= (1 - get_avatar_height_factor(angle))
                current_factor += 0.2
                current_factor = min(1, current_factor)
                avatar_height_factor = current_factor
                target_height = int(max(1, old_height / 2 * avatar_height_factor))
                old_frame = old_frame.resize((int(old_width / 2), target_height))
                target_position = int(old_height / 2 + (1 - avatar_height_factor) * old_height / 2)
                frame.paste(old_frame, (int(old_width / 2), target_position), old_frame)
                rotated_news_paper = newspaper_image.rotate(angle, center=(0, new_newspaper_height))
                frame.paste(rotated_news_paper, (0, 0), rotated_news_paper)
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames, (int(original_input_image.info['duration']) / gif_speedup_factor))
        else:

            frames = []
            logging.info(f'Rendering bonk for static image.')
            input_image = input_image.convert('RGBA')
            current_factor = 1
            for angle in bonk_angles:
                frame = Image.new('RGBA', (old_width, old_height), (0, 0, 0, 0))
                current_factor *= (1 - get_avatar_height_factor(angle))
                current_factor += 0.2
                current_factor = min(1, current_factor)
                avatar_height_factor = current_factor
                target_height = int(max(1, old_height / 2 * avatar_height_factor))
                frame_input_image = input_image.resize((int(old_width / 2), target_height))
                target_position = int(old_height / 2 + (1 - avatar_height_factor) * old_height / 2)
                frame.paste(frame_input_image, (int(old_width / 2), target_position), frame_input_image)
                rotated_news_paper = newspaper_image.rotate(angle, center=(0, new_newspaper_height))
                frame.paste(rotated_news_paper, (0, 0), rotated_news_paper)
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames, 50)

