# FCSModify
Java program to load and edit metadata associated with FCS files such as parameter names.

## About
Flow Cytometry Standard (FCS) files contain metadata along with measured readings for flow cytometry experiments. Sometimes after exporting FCS files you realize you forgot to set the name of one of the parameters. Currently there are no maintained programs that allow you to go back and change them in the FCS file itself. FCSModify is the solution. This program allows you to open FCS3.0 and FCS3.1 files and directly modify attributes. After modification the files can be saves to new FCS files for analysis. 

FCS files are read into a FCS class (htts://github.com/kyle-kroll/FCS). GUI was created in JavaFX and Scene Builder.
