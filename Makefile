CSS=build/css/style.css
APP=build/js/compiled/minimal_canvas.js
IDX=build/index.html
ME=$(shell basename $(shell pwd))
REPO=git@github.com:retrogradeorbit/minimal-canvas.git

all: $(APP) $(CSS) $(IDX)

$(CSS): resources/public/css/style.css
	mkdir -p $(dir $(CSS))
	cp $< $@

$(APP): src/**/** project.clj
	rm -f $(APP)
	lein cljsbuild once min

$(IDX): resources/public/index.html
	cp $< $@

clean:
	lein clean
	rm -rf $(CSS) $(APP) $(IDX)

test-server: all
	cd build && python -m SimpleHTTPServer

setup-build-folder:
	git clone $(REPO) build/
	cd build && git checkout gh-pages

create-initial-build-folder:
	git clone $(REPO) build/
	cd build && git checkout --orphan gh-pages && git rm -rf .
	@echo "now make release build into build/, cd into build and:"
	@echo "git add ."
	@echo "git commit -a -m 'First release'"
	@echo "git push origin gh-pages"

build/node-v6.3.1-linux-x64.tar.xz:
	-mkdir build
	cd build && wget https://nodejs.org/dist/v6.3.1/node-v6.3.1-linux-x64.tar.xz

build/node-v6.3.1-linux-x64: build/node-v6.3.1-linux-x64.tar.xz
	cd build && tar xf node-v6.3.1-linux-x64.tar.xz

export PATH := build/node-v6.3.1-linux-x64/bin:$(PATH)

build/node-v6.3.1-linux-x64/lib/node_modules/uglify-js/bin/uglifyjs: build/node-v6.3.1-linux-x64
	npm install uglify-js -g

resources/public/js/compiled/minimal_canvas.min.js: build/node-v6.3.1-linux-x64/bin/uglifyjs $(APP)
	build/node-v6.3.1-linux-x64/bin/uglifyjs --compress --mangle -- resources/public/js/compiled/minimal_canvas.js > resources/public/js/compiled/minimal_canvas.min.js

resources/public/js/compiled/minimal_canvas.min.js.gz: resources/public/js/compiled/minimal_canvas.min.js
	gzip -9f resources/public/js/compiled/minimal_canvas.min.js
	ls -alF resources/public/js/compiled/minimal_canvas.min.js.gz

release: resources/public/js/compiled/minimal_canvas.min.js.gz
