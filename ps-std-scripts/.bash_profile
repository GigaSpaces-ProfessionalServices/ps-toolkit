# A XAP-maintainer-friendly bash profile.

alias l='ls -lash'
alias cl='clear'

export BD="/opt/xap"
export X12="${BD}/current"

gsas () {
  ps -ef | grep java | grep GSA
}

gscs () {
  ps -ef | grep java | grep GSC
}

webuis () {
  ps -ef | grep java | grep -i webui
}

luss () {
  ps -ef | grep java | grep LH
}

gsms () {
  ps -ef | grep java | grep GSM
}
