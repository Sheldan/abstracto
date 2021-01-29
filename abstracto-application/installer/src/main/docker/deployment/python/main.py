import jinja2
import json
import liquibase_deploy
import os
import sys
import templates_deploy
from zipfile import ZipFile

if len(sys.argv) < 3:
    sys.exit('Wrong amount of parameters.')

use_folder = False
local_folder = None
if len(sys.argv) == 4:
    use_folder = True
    local_folder = sys.argv[3]

deploy_templates = sys.argv[1] == 'yes'
deploy_liquibase = sys.argv[2] == 'yes'

class DbConfig:
    def __init__(self):
        self.host = os.getenv('DB_HOST')
        self.port = os.getenv('DB_PORT')
        self.database = os.getenv('DB_NAME')
        self.user = os.getenv('DB_USER')
        self.password = os.getenv('DB_PASS')

db_config = DbConfig()
if not use_folder:

    postgres_driver_path = os.getenv('POSTGRES_DRIVER')
    liquibase_path = os.getenv('LIQUIBASE_PATH')

    with open('artifact_versions.json') as artifact_config_file:
        artifact_config = json.load(artifact_config_file)



    templateLoader = jinja2.FileSystemLoader(searchpath="/python/templates")
    templateEnv = jinja2.Environment(loader=templateLoader)
    template = templateEnv.get_template("liquibase.properties.j2")

    if deploy_liquibase:

        for liquibase_artifact in artifact_config['liquibase_artifacts']:
            zip_file = liquibase_artifact['zip']
            target_folder = '/liquibase-zips/' + zip_file
            with ZipFile('liquibase-zips/' + zip_file + '.zip', 'r') as liquibase_zip:
                liquibase_zip.extractall(target_folder)
            change_log_file = liquibase_artifact['file']
            liquibase_config_text = template.render(change_log_file=change_log_file, db_host=db_config.host, db_port=db_config.port,
                                                    db_database=db_config.database, db_user=db_config.user, db_password=db_config.password, postgres_driver_path=postgres_driver_path)
            property_path = target_folder + '/liquibase.properties'
            with open(property_path, 'w') as liquibase_target_properties:
                liquibase_target_properties.write(liquibase_config_text)
            liquibase_deploy.deploy_liquibase(zip_file, property_path, liquibase_path)

    if deploy_templates:
        for template_artifact in artifact_config['template_artifacts']:
            with ZipFile('templates/' + template_artifact + '.zip', 'r') as template_zip:
                template_zip.extractall(template_artifact)
            templates_deploy.deploy_template_folder(db_config, template_artifact)

        for template_artifact in artifact_config['translation_artifacts']:
            with ZipFile('translations/' + template_artifact + '.zip', 'r') as template_zip:
                template_zip.extractall(template_artifact)
            templates_deploy.deploy_template_folder(db_config, template_artifact)

if use_folder:
    templates_deploy.deploy_template_folder(db_config, local_folder)