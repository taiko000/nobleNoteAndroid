# nobleNoteAndroid

nobleNote for Android is a simple note taking app. 

Notes can be put into notebooks and notes are saved as html files on the sd card or the internal storage. 
It is compatible to the [nobleNote](https://github.com/hakaishi/nobleNote) desktop application for Linux, Windows and macOS.
The integrated text editor offers some rich text formatting options when text is selected. The nobleNote desktop application offers a full fledged rich text editor. 

It requires at least Android 4.4 to run. 

## Sync notes between desktop and mobile devices

1.) Install [nobleNote](https://github.com/hakaishi/nobleNote) on your Linux/Windows/macOS device and find the folder containing the nobleNote notebooks. 

2.) In nobleNoteAndroid, select a folder on the external storage (sd-card) using the overflow menu on the main screen.

3.) Use a sync software of your choice (e.g. [Syncthing](https://syncthing.net), Dropbox) to sync the folder containining the notebooks with the same folder on your Linux/Windows/macOS device. nobleNote will detect when notes have been changed on the file system and reload them automatically. 

## Screenshots

![Alt text](/screenshot/Screenshot0.png?raw=true "")

![Alt text](/screenshot/Screenshot1.png?raw=true "")

## Build from source

Building from source is not required since you can get it directly on Google Play.

The sources can be build using Android Studio Version 3.0 or greater. 

## License

nobleNoteAndroid is licensed under the MIT License

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
