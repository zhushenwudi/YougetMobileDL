let ENTER_KEYCODE = 13;
let _path = null;
let _pendingReloads = [];
let _reloadingDisabled = 0;
let _device = "目录"
let _ip = null

function formatFileSize(bytes) {
    if (bytes >= 1000000000) {
        return (bytes / 1000000000).toFixed(2) + ' GB';
    }
    if (bytes >= 1000000) {
        return (bytes / 1000000).toFixed(2) + ' MB';
    }
    return (bytes / 1000).toFixed(2) + ' KB';
}

String.prototype.endWith = function (str) {
    if (str == null || str === "" || this.length === 0 || str.length > this.length)
        return false;
    return this.substring(this.length - str.length) === str;
}

function _showError(message, textStatus, errorThrown) {
    $("#alerts").prepend(tmpl("template-alert", {
        level: "danger",
        title: (errorThrown !== "" ? errorThrown : textStatus) + ": ",
        description: message
    }));
}

function _disableReloads() {
    _reloadingDisabled += 1;
}

function _enableReloads() {
    _reloadingDisabled -= 1;

    if (_pendingReloads.length > 0) {
        _reload(_pendingReloads.shift());
    }
}

function _reload(path) {
    if (_reloadingDisabled) {
        if ($.inArray(path, _pendingReloads) < 0) {
            _pendingReloads.push(path);
        }
        return;
    }

    _disableReloads();
    $.ajax({
        url: 'list',
        type: 'GET',
        data: {path: path},
        dataType: 'json'
    }).fail(function (jqXHR, textStatus, errorThrown) {
        _showError("Failed retrieving contents of \"" + path + "\"", textStatus, errorThrown);
    }).done(function (data) {
        const scrollPosition = $(document).scrollTop();

        if (path !== _path) {
            $("#path").empty();
            if (path === "/") {
                $("#path").append('<li class="active">' + _device + '</li>');
            } else {
                $("#path").append('<li data-path="/"><a>' + _device + '</a></li>');
                const components = path.split("/").slice(1, -1);
                for (let i = 0; i < components.length - 1; ++i) {
                    const subpath = "/" + components.slice(0, i + 1).join("/") + "/";
                    $("#path").append('<li data-path="' + subpath + '"><a>' + components[i] + '</a></li>');
                }
                $("#path > li").click(function (event) {
                    _reload($(this).data("path"));
                    event.preventDefault();
                });
                $("#path").append('<li class="active">' + components[components.length - 1] + '</li>');
            }
            _path = path;
        }

        $("#listing").empty();

        for (let i = 0, file; file = data[i]; ++i) {
            $(tmpl("template-listing", file)).data(file).appendTo("#listing");
        }

        $(".edit").editable(function (value) {
            const name = $(this).parent().parent().data("name");
            if (value !== name) {
                const path = $(this).parent().parent().data("path");
                $.ajax({
                    url: 'move',
                    type: 'POST',
                    data: {oldPath: path, newPath: _path + value},
                    dataType: 'json'
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    _showError("Failed moving \"" + path + "\" to \"" + _path + value + "\"", textStatus, errorThrown);
                }).always(function () {
                    _reload(_path);
                });
            }
            return value;
        }, {
            onedit: function () {
                _disableReloads();
            },
            onsubmit: function () {
                _enableReloads();
            },
            onreset: function () {
                _enableReloads();
            },
            tooltip: 'Click to rename...'
        });

        $(".button-download").click(function () {
            let path = $(this).parent().parent().data("path")
            download(path)
        });

        $(".button-open").click(function () {
            const path = $(this).parent().parent().data("path");
            _reload(path);
        });

        $(".button-move").click(function () {
            let path = $(this).parent().parent().data("path");
            if (path[path.length - 1] === "/") {
                path = path.slice(0, path.length - 1);
            }
            $("#move-input").data("path", path);
            $("#move-input").val(path);
            $("#move-modal").modal("show");
        });

        $(".button-play").click(function () {
            let path = $(this).parent().parent().data("path")
            openVideo("download?path=" + encodeURIComponent(path))
        });

        $(".button-delete").click(function () {
            const r = confirm("确认删除文件?");
            if (r === true) {
                const path = $(this).parent().parent().data("path");
                $.ajax({
                    url: 'delete',
                    type: 'POST',
                    data: {path: path},
                    dataType: 'json'
                }).fail(function (jqXHR, textStatus, errorThrown) {
                    _showError("Failed deleting \"" + path + "\"", textStatus, errorThrown);
                }).always(function () {
                    _reload(_path);
                });
            }
        });

        $(document).scrollTop(scrollPosition);
    }).always(function () {
        _enableReloads();
    });
}

function openVideo(src) {
    const page = window.open();
    const html = "<body style='background:black'> <div style='width:80%;margin:auto;'> <video controls width='100%' autoplay src='" + src + "'></video> </div> </body>";
    page.document.write(html);
}

$(document).ready(function () {
    $("#upload-file").click(function () {
        $("#fileupload").click();
    });

    $("#fileupload").click(function (event) {
        event.stopPropagation();
    });

    $("#fileupload").fileupload({
        dropZone: $(document),
        pasteZone: null,
        autoUpload: true,
        sequentialUploads: true,
        url: 'upload',
        type: 'POST',
        dataType: 'json',

        start: function () {
            $(".uploading").show();
        },

        stop: function () {
            $(".uploading").hide();
        },

        add: function (e, data) {
            const file = data.files[0];
            data.formData = {
                file: data.files[0]
            };
            const fileName = file.name;

            if (fileName.indexOf("%") !== -1) {
                alert("暂不支持带有“%”的文件");
                return;
            }
            if (!fileName.endWith(".mp4")) {
                alert("不支持上传非mp4格式文件")
                return
            }
            data.context = $(tmpl("template-uploads", {
                path: _path + file.name
            })).appendTo("#uploads");
            const jqXHR = data.submit();
            data.context.find("button").click(function () {
                jqXHR.abort();
            });
        },

        progress: function (e, data) {
            const progress = parseInt(data.loaded / data.total * 100, 10);
            data.context.find(".progress-bar").css("width", progress + "%");
        },

        done: function (e, data) {
            _reload(_path);
            if (!data) {
                _showError("Failed upload");
            }
        },

        fail: function (e, data) {
            const file = data.files[0];
            if (data.errorThrown !== "abort") {
                _showError("Failed uploading \"" + file.name + "\" to \"" + _path + "\"", data.textStatus, data.errorThrown);
            }
        },

        always: function (e, data) {
            data.context.remove();
        },

    });

    $("#create-input").keypress(function (event) {
        if (event.keyCode === ENTER_KEYCODE) {
            $("#create-confirm").click();
        }
    });

    $("#create-modal").on("shown.bs.modal", function () {
        $("#create-input").focus();
        $("#create-input").select();
    });

    $("#create-folder").click(function () {
        $("#create-input").val("Untitled folder");
        $("#create-modal").modal("show");
    });

    $("#create-confirm").click(function () {
        $("#create-modal").modal("hide");
        const name = $("#create-input").val();
        if (name !== "") {
            $.ajax({
                url: 'create',
                type: 'POST',
                data: {path: _path + name},
                dataType: 'json'
            }).fail(function (jqXHR, textStatus, errorThrown) {
                _showError("Failed creating folder \"" + name + "\" in \"" + _path + "\"", textStatus, errorThrown);
            }).always(function () {
                _reload(_path);
            });
        }
    });

    $("#move-input").keypress(function (event) {
        if (event.keyCode === ENTER_KEYCODE) {
            $("#move-confirm").click();
        }
    });

    $("#move-modal").on("shown.bs.modal", function () {
        $("#move-input").focus();
        $("#move-input").select();
    })

    $("#move-confirm").click(function () {
        $("#move-modal").modal("hide");
        const oldPath = $("#move-input").data("path");
        const newPath = $("#move-input").val();
        if ((newPath !== "") && (newPath[0] === "/") && (newPath !== oldPath)) {
            $.ajax({
                url: 'move',
                type: 'POST',
                data: {oldPath: oldPath, newPath: newPath},
                dataType: 'json'
            }).fail(function (jqXHR, textStatus, errorThrown) {
                _showError("Failed moving \"" + oldPath + "\" to \"" + newPath + "\"", textStatus, errorThrown);
            }).always(function () {
                _reload(_path);
            });
        }
    });

    $("#reload").click(function () {
        _reload(_path);
    });

    $.ajax({
        url: 'getDeviceName',
        type: 'GET',
    }).fail(function (jqXHR, textStatus, errorThrown) {
        _showError("Failed getDeviceName " + textStatus, errorThrown);
    }).done(function (data) {
        _device = JSON.parse(data)['device'];
        _ip = JSON.parse(data)['ip']
        _reload("/");
    });
});

function download(data, strFileName, strMimeType) {
    var self = window, // this script is only for browsers anyway...
        defaultMime = "application/octet-stream", // this default mime also triggers iframe downloads
        mimeType = strMimeType || defaultMime,
        payload = data,
        url = !strFileName && !strMimeType && payload,
        anchor = document.createElement("a"),
        toString = function (a) {
            return String(a);
        },
        myBlob = (self.Blob || self.MozBlob || self.WebKitBlob || toString),
        fileName = strFileName || "download",
        blob,
        reader;
    myBlob = myBlob.call ? myBlob.bind(self) : Blob;

    if (String(this) === "true") { //reverse arguments, allowing download.bind(true, "text/xml", "export.xml") to act as a callback
        payload = [payload, mimeType];
        mimeType = payload[0];
        payload = payload[1];
    }

    if (url && url.length < 2048) { // if no filename and no mime, assume a url was passed as the only argument
        fileName = url.split("/").pop().split("?")[0];
        anchor.href = url; // assign href prop to temp anchor
        if (anchor.href.indexOf(url) !== -1) { // if the browser determines that it's a potentially valid url path:
            var ajax = new XMLHttpRequest();
            ajax.open("GET", url, true);
            ajax.responseType = 'blob';
            ajax.onload = function (e) {
                download(e.target.response, fileName, defaultMime);
            };
            setTimeout(function () {
                ajax.send();
            }, 0); // allows setting custom ajax headers using the return:
            return ajax;
        } // end if valid url?
    } // end if url?

    //go ahead and download dataURLs right away
    if (/^data\:[\w+\-]+\/[\w+\-]+[,;]/.test(payload)) {

        if (payload.length > (1024 * 1024 * 1.999) && myBlob !== toString) {
            payload = dataUrlToBlob(payload);
            mimeType = payload.type || defaultMime;
        } else {
            return navigator.msSaveBlob ?  // IE10 can't do a[download], only Blobs:
                navigator.msSaveBlob(dataUrlToBlob(payload), fileName) :
                saver(payload); // everyone else can save dataURLs un-processed
        }

    }//end if dataURL passed?

    blob = payload instanceof myBlob ?
        payload :
        new myBlob([payload], {type: mimeType});

    function dataUrlToBlob(strUrl) {
        var parts = strUrl.split(/[:;,]/),
            type = parts[1],
            decoder = parts[2] == "base64" ? atob : decodeURIComponent,
            binData = decoder(parts.pop()),
            mx = binData.length,
            i = 0,
            uiArr = new Uint8Array(mx);

        for (i; i < mx; ++i) uiArr[i] = binData.charCodeAt(i);

        return new myBlob([uiArr], {type: type});
    }

    function saver(url, winMode) {
        if ('download' in anchor) { //html5 A[download]
            anchor.href = url;
            anchor.setAttribute("download", fileName);
            anchor.className = "download-js-link";
            anchor.innerHTML = "downloading...";
            anchor.style.display = "none";
            document.body.appendChild(anchor);
            setTimeout(function () {
                anchor.click();
                document.body.removeChild(anchor);
                if (winMode === true) {
                    setTimeout(function () {
                        self.URL.revokeObjectURL(anchor.href);
                    }, 250);
                }
            }, 66);
            return true;
        }

        // handle non-a[download] safari as best we can:
        if (/(Version)\/(\d+)\.(\d+)(?:\.(\d+))?.*Safari\//.test(navigator.userAgent)) {
            url = url.replace(/^data:([\w\/\-\+]+)/, defaultMime);
            if (!window.open(url)) { // popup blocked, offer direct download:
                if (confirm("Displaying New Document\n\nUse Save As... to download, then click back to return to this page.")) {
                    location.href = url;
                }
            }
            return true;
        }

        //do iframe dataURL download (old ch+FF):
        var f = document.createElement("iframe");
        document.body.appendChild(f);

        if (!winMode) { // force a mime that will download:
            url = "data:" + url.replace(/^data:([\w\/\-\+]+)/, defaultMime);
        }
        f.src = url;
        setTimeout(function () {
            document.body.removeChild(f);
        }, 333);

    }//end saver

    if (navigator.msSaveBlob) { // IE10+ : (has Blob, but not a[download] or URL)
        return navigator.msSaveBlob(blob, fileName);
    }

    if (self.URL) { // simple fast and modern way using Blob and URL:
        saver(self.URL.createObjectURL(blob), true);
    } else {
        // handle non-Blob()+non-URL browsers:
        if (typeof blob === "string" || blob.constructor === toString) {
            try {
                return saver("data:" + mimeType + ";base64," + self.btoa(blob));
            } catch (y) {
                return saver("data:" + mimeType + "," + encodeURIComponent(blob));
            }
        }

        // Blob but not URL support:
        reader = new FileReader();
        reader.onload = function (e) {
            saver(this.result);
        };
        reader.readAsDataURL(blob);
    }
    return true;
}
