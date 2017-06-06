# A XAP-maintainer-friendly bash profile.

alias l='ls -lash'
alias cl='clear'
export X12=/opt/xap

gsas () {
  ps -ef | grep GSA
}

gscs () {
  ps -ef | grep GSC
}

luss () {
  ps -ef | grep LH
}

webuis() {
  ps -ef | grep -i webui
}
