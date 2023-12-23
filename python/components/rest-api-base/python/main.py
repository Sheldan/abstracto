import logging
import os

from flask import Flask
import importlib

FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
logging.basicConfig(encoding='utf-8', level=logging.INFO, format=FORMAT)
template_dir = os.path.abspath('resources/templates')
app = Flask(__name__, template_folder=template_dir)

import sys
sys.path.append("..")

# This code was only done, because "from custom import *" did not work, it seems it did not execute the code in it
# while the code was valid
# https://stackoverflow.com/questions/57878744/how-do-i-dynamically-import-all-py-files-from-a-given-directory-and-all-sub-di
def get_py_files(src):
    cwd = os.getcwd()
    py_files = []
    for root, dirs, files in os.walk(src):
        for file in files:
            if file.endswith(".py"):
                py_files.append(os.path.join(cwd, root, file))
    return py_files


def dynamic_import(module_name, py_path):
    module_spec = importlib.util.spec_from_file_location(module_name, py_path)
    module = importlib.util.module_from_spec(module_spec)
    module_spec.loader.exec_module(module)
    return module


def dynamic_import_from_src(src, star_import=False):
    my_py_files = get_py_files(src)
    for py_file in my_py_files:
        module_name = os.path.split(py_file)[-1].strip(".py")
        imported_module = dynamic_import(module_name, py_file)
        if star_import:
            for obj in dir(imported_module):
                globals()[obj] = imported_module.__dict__[obj]
        else:
            globals()[module_name] = imported_module
    return


if __name__ == "__main__":
    dynamic_import_from_src("endpoints", star_import=False)

@app.route('/')
def hello():
    return 'Hello, World?'


if __name__ == "__main__":
    from waitress import serve

    serve(app, host="0.0.0.0", port=8080)
