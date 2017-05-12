# Project settings.
project(SCS NONE)
cmake_minimum_required(VERSION 3.4.3)
list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

# Add latex macros (https://github.com/kmorel/UseLATEX)
include(cmake/UseLATEX.cmake)

# The main target.
add_latex_document(
    scs.tex
    BIBFILES bibliography.bib
)
