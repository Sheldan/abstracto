import sqlalchemy as db
import glob
import os
from sqlalchemy.sql import text


def deploy_template_folder(db_config, folder):
    engine = db.create_engine('postgres://%s:%s@%s:%s/%s' % (db_config.user, db_config.password, db_config.host, db_config.port, db_config.database))

    if not os.path.isdir(folder):
        print("Given path was not a folder. Exiting.")
        exit(1)

    files = glob.glob(folder + '/**/*.ftl', recursive=True)
    templates = []
    for file in files:
        with open(file) as template_file:
            file_content = template_file.read()
            template_key = os.path.splitext(os.path.basename(file))[0]
            template = {'key': template_key, 'content': file_content}
            templates.append(template)

    print('Deploying %s templates from folder %s' % (len(templates), folder))

    with engine.connect() as con:
        statement = text("""INSERT INTO template(key, content, last_modified) VALUES(:key, :content, NOW()) ON CONFLICT (key) DO UPDATE SET content = :content""")

        for line in templates:
            con.execute(statement, **line)
