from __main__ import app

from PIL import Image, ImageOps
from flask import request
import requests
import validators
import logging
import io

from utils import flask_utils


sprite_size = 112
sprites = 5
allowed_content_length = 4_000_000
allowed_image_formats = ['image/png', 'image/jpeg', 'image/jpg', 'image/gif']


@app.route('/memes/pat/file.gif')  # to directly embed, discord _requires_ this file ending, it seems
def pat_animated():
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
    frame_count = original_input_image.n_frames
    old_width, old_height = input_image.size
    with Image.open('resources/img/pat_sprite.png') as pat_background:
        pat_background = pat_background.convert('RGBA')
        pat_width, pat_height = pat_background.size
        pat_ratio = old_width / sprite_size
        original_background_images = []
        for i in range(sprites):
            background_part = pat_background.crop((i * sprite_size, 0, (i + 1) * sprite_size, pat_height))
            original_background_images.append(background_part)
        desired_new_pat_width = int(sprite_size * pat_ratio)
        resized_background_images = []
        for background_image in original_background_images:
            if pat_ratio > 1:
                resized_background = background_image.resize((desired_new_pat_width, pat_height))
            else:
                resized_background = ImageOps.contain(background_image, (desired_new_pat_width, pat_height))
            resized_background_images.append(resized_background)
        if content_type == 'image/gif':
            logging.info(f'Rendering pet for gif.')
            old_frames = []
            for frame_index in range(frame_count):
                input_image.seek(frame_index)
                frame = input_image.convert('RGBA')
                old_frames.append(frame)
            frames = []
            for index, old_frame in enumerate(old_frames):
                frame = Image.new('RGBA', (old_width, old_height), (0, 0, 0, 0))
                frame.paste(old_frame, (0, 0), old_frame)
                frame_background_image = resized_background_images[index % sprites]
                frame.paste(frame_background_image, (0, 0), frame_background_image)
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames, int(original_input_image.info['duration']))
        else:
            frames = []
            logging.info(f'Rendering pet for static image.')
            input_image = input_image.convert('RGBA')
            for background_image in resized_background_images:
                frame = Image.new('RGBA', (old_width, old_height), (0, 0, 0, 0))
                frame.paste(input_image, (0, 0), input_image)
                frame.paste(background_image, (0, 0), background_image)
                frames.append(frame)
            return flask_utils.serve_pil_gif_image(frames, 100)
