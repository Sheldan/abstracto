import json
import os
import sys
import templates_deploy
from zipfile import ZipFile
import logging

FORMAT = '%(asctime)s - %(name)s - %(levelname)s - %(message)s'
logging.basicConfig(encoding='utf-8', level=logging.INFO, format=FORMAT)

use_folder = False
local_folder = None
config_dir = sys.argv[1]
if len(sys.argv) == 3:
    use_folder = True
    local_folder = sys.argv[2]


class DbConfig:
    def __init__(self):
        self.host = os.getenv('DB_HOST')
        self.port = os.getenv('DB_PORT')
        self.database = os.getenv('DB_NAME')
        self.user = os.getenv('DB_USER')
        self.password = os.getenv('DB_PASS')
        self.scheme = os.getenv('DB_SCHEME')


db_config = DbConfig()
if not use_folder:
    logging.info("Not deploying with folder.")
    logging.info("Loading versions.")
    with open(config_dir + 'artifact_versions.json') as artifact_config_file:
        artifact_config = json.load(artifact_config_file)

    logging.info("Deploying templates.")
    for template_artifact in artifact_config['template_artifacts']:
        folder_name = config_dir + '/' + template_artifact + "-templates"
        os.mkdir(folder_name)
        with ZipFile(config_dir + 'templates/' + template_artifact + '.zip', 'r') as template_zip:
            template_zip.extractall(folder_name)
        templates_deploy.deploy_template_folder(db_config, folder_name)

    logging.info("Deploying translation templates")
    for template_artifact in artifact_config['translation_artifacts']:
        folder_name = config_dir + '/' + template_artifact + "-translations"
        with ZipFile(config_dir + 'translations/' + template_artifact + '.zip', 'r') as template_zip:
            template_zip.extractall(folder_name)
        templates_deploy.deploy_template_folder(db_config, folder_name)

if use_folder:
    logging.info("Only deploying folder.")
    templates_deploy.deploy_template_folder(db_config, local_folder)