import os


def deploy_liquibase(folder, change_log_file, liquibase_path):
    stream = os.popen('cd liquibase-zips/%s && %s/liquibase --defaultsFile=%s --liquibaseSchemaName=abstracto --liquibaseCatalogName=abstracto --logLevel=info update' % (folder, liquibase_path, change_log_file))
    output = stream.read()
    print(output)
