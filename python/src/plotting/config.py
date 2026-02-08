TARGET_PATH = r'/home/user/results/images'
DATABASE_PATH = r'/home/user/java/build/resources/main/database/results.db'
SYSTEMS = ['argouml-spl', 'berkeley-db-libdb', 'busybox', 'dia', 'apache-httpd', 'irssi', 'libssh', 'openvpn', 'vim']
METRICS = ['f1', 'precision', 'recall']
VARIANT_NUMBERS = [3, 5, 7]
# replication experiment settings
TRACE_PERCENTAGES = [0, 5, 10, 15, 20, 25]
# robustness experiment settings
MISTAKE_TYPES = ["SwappedCondition", "ErroneousConjunction", "SwappedFeature"]
MISTAKE_PERCENTAGES = [25, 50, 75, 100] # excluding 0