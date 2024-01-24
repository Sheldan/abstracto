from __main__ import app

from PIL import Image, ImageOps
from flask import request
import logging
import math
import re
import random

from utils import flask_utils

vertical_padding = 5
horizontal_padding = 5

max_chars = 2000
chars_per_row = 50


space_width = 80

character_height = 120


@app.route('/memes/amogus/text')
def generate_amogus_text():
    text = request.args.get('text', type=str)
    if text is None:
        return 'no text', 400
    if len(text) > max_chars:
        return f'too long text, max {max_chars}', 400
    text = text.lower()
    text = re.sub(r'[^a-z!\\? ]', "", text)
    if len(text) == 0:
        return 'No valid characters found.', 400
    logging.info(f'Rendering amogus text.')
    image_cache = {}
    images_to_use = []

    try:
        for character in text:
            if character == ' ':
                images_to_use.append(None)
                continue
            # manual correction for filenames
            if character == '?':
                character = 'zq'
            if character == '!':
                character = 'zx'
            chosen_sub_image = random.randint(1, 3)
            cache_key = f'{character}{chosen_sub_image}'
            if cache_key not in image_cache:
                character_image = Image.open(f'resources/img/amogus/crewmate-{character}{chosen_sub_image}.png').__enter__()
                old_character_width, old_character_height = character_image.size
                character_ratio = old_character_height / character_height
                desired_width = int(old_character_width * character_ratio)
                resized_character_image = ImageOps.contain(character_image, (desired_width, character_height))
                image_cache[cache_key] = resized_character_image
                image_to_use = resized_character_image
            else:
                image_to_use = image_cache[cache_key]
            images_to_use.append(image_to_use)
        # the line length is defined by the amount of characters, not once a certain width is reached
        lines = [images_to_use[i:i + chars_per_row] for i in range(0, len(images_to_use), chars_per_row)]
        # calculate the length of each line, and then take the widest one
        line_widths = [sum(image_to_use.size[0] + horizontal_padding if image_to_use is not None else space_width for image_to_use in character_line) for character_line in lines]
        canvas_width = max(line_widths)
        row_count = math.ceil(len(text) / chars_per_row)
        canvas_height = row_count * (character_height + vertical_padding)
        drawing_board = Image.new('RGBA', (canvas_width, canvas_height), (0, 0, 0, 0))
        start_x = 0
        start_y = 0
        for index, image_to_use in enumerate(images_to_use):
            if index > 0 and (index % chars_per_row) == 0:
                start_y += character_height + vertical_padding
                start_x = 0
            if image_to_use is not None:
                drawing_board.paste(image_to_use, (start_x, start_y), image_to_use)
                start_x += image_to_use.size[0] + horizontal_padding
            else:
                start_x += space_width
    finally:
        for cached_image in image_cache.values():
            cached_image.__exit__(None, None, None)
    return flask_utils.serve_pil_image(drawing_board)

