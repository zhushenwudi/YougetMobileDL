import sys
from io import StringIO

sys.stdout = sys.__stdout__
from you_get import common as you_get


def getJson(url):
    buffer = StringIO()
    try:
        sys.stdout = buffer
        sys.argv = ['you-get', url, '--json', '--debug']
        you_get.main()
        res = buffer.getvalue()
        return res
    except:
        return "error"
    finally:
        buffer.close()
