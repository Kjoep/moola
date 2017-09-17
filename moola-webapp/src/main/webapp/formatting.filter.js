// for some reason, this does not work when I use 'formatPeer'.  Still need to figure out why
angular.module('moola')

    .filter('format_Peer', [function () {
        return function (transaction) {
            if (transaction.peer) return transaction.peer.name;
            else if (transaction.peerInfo) return '? ' + transaction.peerInfo.name;
            else if (transaction.terminalInfo) return '? ' + transaction.terminalInfo.name + " " + transaction.terminalInfo.location;
            else return '?';
        }
    }])
    .filter('formatPeerLong', [function () {
        return function (transaction) {
            var r = ''
            if (transaction.peer) r = transaction.peer.name + ' :: ';

            if (transaction.peerInfo) return r + transaction.peerInfo.name + ' (' + transaction.peerInfo.account + ')';
            if (transaction.terminalInfo) return r + transaction.terminalInfo.name + " " + transaction.terminalInfo.location + ' (card ' + transaction.terminalInfo.card + ')';
            else return r;
        }
    }])
    .filter('formatPeerClass', [function () {
        return function (transaction) {
            if (transaction.peer) return 'peer-' + transaction.peer.class;
            else return 'peer-unknown';
        }
    }]);

