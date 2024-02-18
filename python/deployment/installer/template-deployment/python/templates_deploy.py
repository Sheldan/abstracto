import glob
import os
import sqlalchemy as db
from sqlalchemy.sql import text
import logging


def deploy_template_folder(db_config, folder):
    engine = db.create_engine('postgresql://%s:%s@%s:%s/%s' % (db_config.user, db_config.password, db_config.host, db_config.port, db_config.database))

    if not os.path.isdir(folder):
        logging.warn(f'Given path {folder} was not a folder. Exiting.')
        exit(1)

    files = glob.glob(folder + '/**/*.ftl', recursive=True)
    templates = []
    for file in files:
        with open(file) as template_file:
            file_content = template_file.read()
            template_key = os.path.splitext(os.path.basename(file))[0]
            template = {'key': template_key, 'content': file_content}
            logging.debug(f'Deployment template {template}')
            templates.append(template)

    logging.info(f'Deploying {len(templates)} templates from folder {folder}')

    with engine.connect() as con:
        with con.begin():
            statement = text(f"""INSERT INTO {db_config.scheme}template(key, content, last_modified) VALUES(:key, :content, NOW()) ON CONFLICT (key) DO UPDATE SET content = :content""")

            for line in templates:
                con.execute(statement, parameters=line)
