import subprocess
import sys


def deploy_liquibase(folder, change_log_file, liquibase_path, base_path, db_config):
    print(f'Deploying liquibase of {change_log_file} in folder {folder}')
    process_command = [f'{liquibase_path}/liquibase', f'--defaultsFile={change_log_file}', f'--defaultSchemaName={db_config.scheme}', f'--liquibaseSchemaName={db_config.scheme}',  f'--liquibaseCatalogName={db_config.scheme}', '--logLevel=info', 'update']
    process = subprocess.Popen(process_command,
                     cwd=f'{base_path}/liquibase-zips/{folder}',
                     stderr=sys.stderr,
                     stdout=sys.stdout)

    process.communicate()
    code = process.returncode
    if code != 0:
        print("Liquibased deployment failed.")
        sys.exit(code)