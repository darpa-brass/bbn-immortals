#!/usr/gin/python3
#
# https://docs.python.org/3/library/http.server.html
#
# The actual server source code is found...
#   immortals-svn/das/das-service/src/main/
#      java/mil/darpa/immortals/core/
#          das/restendpoints/DASEndpoint.java
#
# For a good example see...
#   https://gist.github.com/touilleMan/eb02ea40b93e52604938
#
__version__ = "0.1"
__all__ = ["mock_mission_server"]
__author__ = "fred.eisele@vanderbilt.edu"
__home_page__ = "http://immortals.isis.vanderbilt.edu:3000/"

from bottle import Bottle, route, run, template, request, response, error
import os
import cgi

app = Bottle()

@app.error(404)
def error404(error):
    return 'you got a 404'

@app.route('/')
@app.route('/hello/<name>')
def hello(name='Stranger'):
    return template('<b>Hello {{name}}</b>!', name=name)

@app.route('/login', method='GET')
def loginGet():
    return '''\
<html>
<body>
<form action="/login" method="post">
Username: <input name="username" type="text" />
Password: <input name="password" type="password" />
<input value="Login" type="submit" />
</form>
</body>
</html>
'''

@app.route('/login', method='POST')
def loginPost():
    username = request.forms.get('username')
    return template('<b>Hello {{username}}</b>!', username=username)

@app.route('/deployment-model', method='GET')
def deployGet():
    return '''\
<html>
<body>
<form action="/deployment-model" method="post" enctype="multipart/form-data">
File: <input name="filename" type="file" id="payload"/>
<input value="Upload" type="submit" />
</form>
</body>
</html>
'''

@app.route('/deployment-model', method='POST')
def deployPostFiles():
    try:
        payload = request.files['filename']
        name, ext = os.path.splitext(payload.filename)
        payload.save(payload.filename)
        print( template('deployed file: {{name}}', name=payload.filename) )
        return template('<html><b>deployed file: {{name}}</b>!</html>',
            name=payload.filename)
    except:
        print('could not handle request')
        return '<html><body><b>could not handle request</b>!</body></html>',

#
# This mimics 'curl --data filename.txt' behavior
# https://www.html5rocks.com/en/tutorials/file/xhr2/
#
@app.route('/das/deployment-model', method='GET')
def deployGet():
    return '''\
<html>
<head>

    <style>
    #afile {
      width: 120px;
    }
    #afile:before {
      content: 'Select a TTL file';
    }
    </style>
</head>
<body>
    <div>
    #bytes/chunk: <input type="number" min="1048576" value="1048576" id="numChunks">
    <input type="file" id="afile" class="button">
    <div id="bars"></div>
  </div>
</body>

<script>
(function() {
    var progress = document.querySelector('progress');
    var bars = document.querySelector('#bars');

    var uploaders = [];

    function upload(blobOrFile) {
        var progress = document.createElement('progress');

        progress.min = 0;
        progress.max = 1;
        progress.value = 0;
        bars.appendChild(progress);

        var xhr = new XMLHttpRequest();
        xhr.open('POST', '/das/deployment-model', true);
        xhr.onload = function(e) {
            progress.value = 1; // Insure final value is 100%.

            uploaders.pop();

            if (!uploaders.length) {
                bars.appendChild(document.createElement('br'));
                bars.appendChild(document.createTextNode('Upload Complete'));
            }
        };

        // Listen to the upload progress for each upload.
        xhr.upload.onprogress = function(e) {
            if (e.lengthComputable) {
                progress.value = e.loaded / e.total;
                progress.textContent = parseFloat(progress.value) * 100; // Fallback for unsupported browsers.
            }
        };
        uploaders.push(xhr);
        xhr.send(blobOrFile);
    }

    document.querySelector('#afile').addEventListener('change', function() {
        var blob = this.files[0];

        const BYTES_PER_CHUNK = parseInt(document.querySelector('#numChunks').value);
        const SIZE = blob.size;
        const NUM_CHUNKS = Math.max(Math.floor(SIZE / BYTES_PER_CHUNK), 1);

        // Reset from previous runs.
        bars.innerHTML = '';
        bars.innerHTML = 'Sending <b>' + NUM_CHUNKS + '</b> chunks:<br>';

        var start = 0;
        var end = BYTES_PER_CHUNK;

        while(start < SIZE) {
            upload(blob.slice(start, end));

            start = end;
            end = start + BYTES_PER_CHUNK;
        }
    }, false);

})();
</script>
</html>
'''

@app.route('/das/deployment-model', method='POST')
def deployPost():
    payload = request.body.getvalue()
    contDisp = request.get_header('Content-Disposition', 'attachment; filename=uploaded.ttl')
    value, params = cgi.parse_header(contDisp)
    with open(params['filename'], 'wb') as f:
        f.write(payload)
    return template('<html><b>deployed file of size: {{size}}</b>!</html>',
        size=len(payload))


print("running...")
run(app, host='localhost', port=8088)
