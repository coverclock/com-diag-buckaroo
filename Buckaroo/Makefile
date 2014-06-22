################################################################################
# Copyright 2014 by the Digital Aggregates Corporation, Colorado, USA
# Licensed under the terms in README.txt
# Chip Overclock <coverclock@diag.com>
# http://www.diag.com/navigation/downloads/Buckaroo
################################################################################

PROJECT		=	buckaroo
TITLE		=	Buckaroo
SYMBOL		=	BUCKAROO

SVN_REPO	=	svn://graphite/$(PROJECT)
SVN_URL		=	${SVN_REPO)/trunk/$(TITLE)
HTTP_URL	=	http://www.diag.com/navigation/downloads/$(TITLE).html
GIT_URL		=	https://github.com/coverclock/com-diag-$(PROJECT).git

.PHONY:	commit dcommit push init

commit:
	git commit .

dcommit:
	git svn dcommit

push:
	git push origin master

init:
	git svn clone -s $(SVN_REPO)
	git svn show-ignore > .gitignore
	git remote add origin $(GIT_URL)
