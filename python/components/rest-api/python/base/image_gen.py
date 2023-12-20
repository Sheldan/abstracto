from __main__ import app

from PIL import Image, ImageDraw, ImageFont
from flask import request

from utils import flask_utils

@app.route('/memes/doge/orangeSun/')
def image_gen():
    text = request.args.get('text')
    with Image.open('resources/img/semf_template.jpg') as im:
        d1 = ImageDraw.Draw(im)
        text_box_size = (300, 240)
        W, H = text_box_size
        font = ImageFont.truetype(f'Impact.ttf', 60)
        _, _, w, h = d1.textbbox((0, 0), text, font=font)
        d1.text(((W-w)/2 + 320, (H-h)/2 + 120), text, font=font, fill=(255, 255, 255))
        return flask_utils.serve_pil_image(im)
