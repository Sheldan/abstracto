from __main__ import app

from PIL import Image, ImageDraw, ImageFont
from flask import request
import logging

from utils import flask_utils

@app.route('/memes/grrr/text')
def grrr():
    text = request.args.get('text')
    if text is None:
        return 'no text', 400
    logging.info(f'Rendering grrr.')
    with Image.open('resources/img/grrr.png') as im:
        d1 = ImageDraw.Draw(im)
        text_box_size = (250, 200)
        W, H = text_box_size
        font = ImageFont.truetype(f'Impact.ttf', 60)
        _, _, w, h = d1.textbbox((0, 0), text, font=font)
        d1.text(((W-w)/2 + 120, (H-h)/2 + 330), text, font=font, fill=(255, 255, 255))
        _, _, w2, h2 = d1.textbbox((0, 0), 'grrr', font=font)
        d1.text(((W-w2)/2 + 100, (H-h2)/2 - 90), 'grrr', font=font, fill=(255, 255, 255))
        return flask_utils.serve_pil_image(im)


