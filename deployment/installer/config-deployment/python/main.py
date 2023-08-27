import jinja2
import json
import os
import re
import liquibase_deploy
import sys
from zipfile import ZipFile


class DbConfig:
    def __init__(self):
        self.host = os.getenv('DB_HOST')
        self.port = os.getenv('DB_PORT')
        self.database = os.getenv('DB_NAME')
        self.user = os.getenv('DB_USER')
        self.password = os.getenv('DB_PASS')
        self.scheme = os.getenv('DB_SCHEME')


config_dir = sys.argv[1]

db_config = DbConfig()
postgres_driver_path = os.getenv('POSTGRES_DRIVER', '/postgres/driver.jar')
liquibase_path = os.getenv('LIQUIBASE_PATH', '/liquibase')
script_directory = os.path.dirname(os.path.abspath(sys.argv[0]))

print("Loading versions.")
with open(config_dir + 'artifact_versions.json') as artifact_config_file:
    artifact_config = json.load(artifact_config_file)

print("Loading templates")
templateLoader = jinja2.FileSystemLoader(searchpath="/python/templates")
templateEnv = jinja2.Environment(loader=templateLoader)
variable_prefix_pattern = re.compile(r'ABSTRACTO_\w+')
variables = {}
for key, val in os.environ.items():
    if variable_prefix_pattern.match(key):
        variables[key.lower().replace('_', '')] = val

template = templateEnv.get_template("liquibase.properties.j2")

print("Starting liquibase deployment")
for liquibase_artifact in artifact_config['liquibase_artifacts']:
    zip_file = liquibase_artifact['zip']
    target_folder = config_dir + '/liquibase-zips/' + zip_file
    with ZipFile(config_dir + 'liquibase-zips/' + zip_file + '.zip', 'r') as liquibase_zip:
        liquibase_zip.extractall(target_folder)
    change_log_file = liquibase_artifact['file']
    liquibase_config_text = template.render(change_log_file=change_log_file, db_host=db_config.host, db_port=db_config.port,
                                            db_database=db_config.database, db_user=db_config.user, db_password=db_config.password,
                                            postgres_driver_path=postgres_driver_path, variables=variables)
    property_path = script_directory + '/templates/liquibase.properties'
    with open(property_path, 'w') as liquibase_target_properties:
        liquibase_target_properties.write(liquibase_config_text)
    liquibase_deploy.deploy_liquibase(zip_file, property_path, liquibase_path, config_dir, db_config)
