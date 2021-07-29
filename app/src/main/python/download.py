import sys
import traceback

so = sys.stdout
sys.stdout = sys.__stdout__
from you_get import common as you_get
sys.stdout = so

def download(url, path, name):
    try:
        sys.argv = ['you-get', url, '-o', path, '--no-caption', '-O', name, '--debug']
        you_get.main()
        return "finish"
    except:
        traceback.print_exc()
        return "error"
