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


def pretty_column_name(column_name: str):
    match column_name:
        case 'dataset':
            return 'Subject System'
        case 'metric':
            return 'Metric'
        case 'number_of_variants':
            return '#Variants'
        case 'ft_percentage':
            return '% Feature Traces'
        case 'shapiro':
            return 'Shapiro'
        case 'ttest':
            return 'T-Test'
        case 'wilcoxon':
            return 'Wilcoxon'


def pretty_dataset_name(dataset: str):
    match dataset:
        case 'apache-httpd':
            return 'Apache HTTP'
        case 'argouml-spl':
            return 'ArgoUML'
        case 'berkeley-db-libdb':
            return 'Berkeley DB'
        case 'dia':
            return 'Dia'
        case 'openvpn':
            return 'OpenVPN'
        case 'libssh':
            return 'LibSSH'
        case 'irssi':
            return 'Irssi'
        case 'vim':
            return 'Vim'
        case 'busybox':
            return 'BusyBox'
