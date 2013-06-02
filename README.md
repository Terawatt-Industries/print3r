# BigSlice Slicing Server

BigSlice is a server which provides 3D object STL -> GCODE translation, also known as "slicing".  BigSlice runs on any OS that supports Java such as Linux, OS X, and Windows.

BigSlice provides a server with a drag-and-drop feature for STL file slicing.  To use it:
- Install and configure the BigSlice server on a machine on a (trusted) network
- Drag a STL file to the data/stldrop folder
- Retrieve GCODE from the data/out folder
- Multithreaded slicing - limited to 2 instances at a time.

See the [wiki](wiki) for installation and development instructions.

BigSlice allows you to drag-and-drop STL files into a folder on your computer and automatically outputs GCode.  It's designed to work with Slic3r but it can be easily adapted to work with any slicing software which provides command-line-interface (CLI) execution.

If you run this on your laptop/desktop, or a computer on a trusted network (or cloud server); then you would share the "data/stldrop" and "data/out" folders; then drag STL files to data/stldrop; and finally retrieve output gcode from data/out/...

Why not just use Slic3r for this, for example?  BigSlice is designed for a use-case that's fairly common for 3D printer users:  slicing a single STL file using multiple slicing (Slic3r) configs.  When the server starts, it scans for Slic3r INIs and stores them.  It then uses each INI for slicing STL -> GCode; and outputs the STL filename tagged with the name of the INI file.

