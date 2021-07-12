import os
import sys

so = sys.stdout
sys.stdout = sys.__stdout__
from you_get import common as you_get

sys.stdout = so


def download(url, path):
    try:
        you_get.any_download(url, info_only=False, output_dir=os.path.abspath(path), merge=True)
        return "finish"
    except:
        return "error"
