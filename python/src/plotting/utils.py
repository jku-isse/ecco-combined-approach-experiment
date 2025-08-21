def boolean2int(boolean: bool):
    return 1 if boolean else 0

def dataset_to_file_name(dataset: str):
    match dataset:
        case 'argouml-spl':
            return 'argouml'
        case 'berkeley-db-libdb':
            return 'berkeley'
        case 'apache-httpd':
            return 'httpd'
        case _:
            return dataset
