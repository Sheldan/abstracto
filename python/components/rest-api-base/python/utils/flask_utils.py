from io import BytesIO

from flask import send_file


def serve_pil_image(pil_img):
    img_io = BytesIO()
    pil_img.save(img_io, 'PNG')
    img_io.seek(0)
    return send_file(img_io, mimetype='image/png')


def serve_pil_gif_image(frames, duration=25):
    animated_gif = BytesIO()
    frames[0].save(animated_gif, format='GIF', save_all=True, append_images=frames[1:], duration=duration, loop=0, disposal=2)
    animated_gif.seek(0)
    return send_file(animated_gif, mimetype='image/gif')


class ValidationException(Exception):
    def __init__(self, provided_value, message):
        self.provided_value = provided_value
        self.message = message
        super().__init__(f'{self.message}: provided value: {provided_value}')
