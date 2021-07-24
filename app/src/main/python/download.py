import sys
import traceback

so = sys.stdout
sys.stdout = sys.__stdout__
from you_get import common as you_get
sys.stdout = so

def download(url, path):
    try:
        sys.argv = ['you-get', url, '-o', path, '--no-caption', '--debug']
        you_get.main()
        # you_get.any_download(url, info_only=False, output_dir=os.path.abspath(path), merge=None, debug=True)
        return "finish"
    except:
        traceback.print_exc()
        return "error"

