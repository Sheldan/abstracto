import subprocess
import sys


def deploy_liquibase(folder, change_log_file, liquibase_path):
    print("Deploying liquibase of %s in folder %s" % (change_log_file, folder))
    process = subprocess.Popen(['%s/liquibase' %  (liquibase_path), '--defaultsFile=%s' % (change_log_file), '--liquibaseSchemaName=abstracto',  '--liquibaseCatalogName=abstracto', '--logLevel=info', 'update'],
                     cwd='liquibase-zips/%s' % (folder),
                     stderr=sys.stderr,
                     stdout=sys.stdout)

    process.communicate()
    code = process.returncode
    if code != 0:
        print("Liquibased deployment failed.")
        sys.exit(code)