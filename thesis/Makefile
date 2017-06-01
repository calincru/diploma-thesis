# too many dependencies, marking targets as .PHONY
# to be revised in the future

.PHONY: build
build: thesis.pdf clean-tmp

.PHONY: thesis.pdf
thesis.pdf:
	pdflatex -shell-escape -interaction=scrollmode --src-specials thesis.tex
	bibtex thesis
	makeindex thesis.nlo -s nomencl.ist -o thesis.nls
	pdflatex -shell-escape -interaction=scrollmode --src-specials thesis
	makeindex thesis.idx
	@echo; echo; echo
	@echo "****************************************************"
	@echo "***** Ignore errors above. Check errors below. *****"
	@echo
	pdflatex -shell-escape -interaction=scrollmode --src-specials thesis

.PHONY: clean-tmp
clean-tmp:
	rm *.log *.lot *.lof *.blg *.bbl *.out *.toc *.aux &>/dev/null || true
	find . -name *.aux | xargs rm &>/dev/null || true
	rm thesis.idx thesis.ilg thesis.ind &>/dev/null || true
	rm thesis.nlo thesis.nls &>/dev/null || true

.PHONY: clean
clean: clean-tmp
	rm thesis.pdf &>/dev/null || true
