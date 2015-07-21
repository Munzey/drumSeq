prerequisites: Java 8+

**To run:**
clone the repository, open the folder and run (on *NIX machines) `./DrumSeq run`, or (on windows) `./DrumSeq.bat run`

The src folder hierarchy has been kept to standard with processing guidelines.

**Known Issues**
In current state if you have no internet connection(but have all the dependencies already downloaded) you will need to run `./DrumSeq run --offline` instead

Tested on linux, however im sure i found a known issue with pixel position between linux and mac, perhaps im wrong and the issue is with how im scaling everything. Edit: after seeing on a mac there is an issue with pshape object positions