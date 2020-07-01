import sqlalchemy as db
import glob
import os
import sys
from sqlalchemy.sql import text

host = os.getenv('DB_HOST')
port = os.getenv('DB_PORT')
database = os.getenv('DB_NAME')
user_name = os.getenv('DB_USER')
password = os.getenv('DB_PASS')
folder = sys.argv[1]

engine = db.create_engine('postgres://%s:%s@%s:%s/%s' % (user_name, password, host, port, database))

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
