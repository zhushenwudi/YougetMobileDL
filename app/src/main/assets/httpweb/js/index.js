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
            let name = $(this).parent().parent().data("name")
            let a = document.createElement('a')
            a.href = _ip + "/download?path=" + encodeURIComponent(path)
            a.download = name
            a.click()
            a.remove()
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
            window.location = "download?path=" + encodeURIComponent(path);
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
