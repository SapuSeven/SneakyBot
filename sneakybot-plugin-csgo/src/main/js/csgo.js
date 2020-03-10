const SteamUser = require('steam-user')
const GlobalOffensive = require('globaloffensive')
const SteamID = require('steamid')

const log = require("./log.js")
const config = require("./config.js")

const client = new SteamUser()
const csgo = new GlobalOffensive(client)


let friendProfiles = {}

function limiter(fn, wait){
    let isCalled = false,
        calls = [];

    let caller = function(){
        if (calls.length && !isCalled){
            isCalled = true;
            calls.shift().call();
            setTimeout(function(){
                isCalled = false;
                caller();
            }, wait);
        }
    };

    return function(){
        calls.push(fn.bind(this, ...arguments));
        caller();
    };
}

function loadProfileUnlimited(sid) {
	log("INFO", "Loading CS:GO profile for user " + sid)
	csgo.requestPlayersProfile(sid)
}

const loadProfile = limiter(sid => {loadProfileUnlimited(sid)}, 1000)

function loadFriendProfiles() {
	Object.keys(client.myFriends).forEach((sid) => {
		loadProfile(sid)
	})
}

client.on('loggedOn', function(details) {
	log("INFO", "Login successful. SteamID: " + client.steamID)
	log("INFO", "Use code " + client.steamID.accountid + " to add the bot as a friend")
	client.setPersona(SteamUser.EPersonaState.Online)
	// For new accounts, implement and use requestFreeLicense to request a free CS:GO license
	client.gamesPlayed(730, true)
})

client.on('error', function(e) {
	// Some error occurred during logon
	log("ERROR", e);
})

client.on('webSession', function(sessionID, cookies) {
	log("STATUS", "Got web session");
	// Do something with these cookies if you wish
})

client.on('newItems', function(count) {
	log("STATUS", count + " new items in inventory")
})

client.on('emailInfo', function(address, validated) {
	log("STATUS", "E-Mail address: " + address + " " + (validated ? "(validated)" : "(not validated)"))
})

client.on('wallet', function(hasWallet, currency, balance) {
	log("STATUS", "Wallet balance: " + SteamUser.formatCurrency(balance, currency))
})

client.on('accountLimitations', function(limited, communityBanned, locked, canInviteFriends) {
	var limitations = []

	if (limited) {
		limitations.push('LIMITED')
	}

	if (communityBanned) {
		limitations.push('COMMUNITY BANNED')
	}

	if (locked) {
		limitations.push('LOCKED')
	}

	if (limitations.length === 0) {
		log("STATUS", "Account has no limitations.")
	} else {
		log("STATUS", "Account is " + limitations.join(', ') + ".")
	}

	if (canInviteFriends) {
		log("STATUS", "Account can invite friends.")
	}
})

client.on('vacBans', function(numBans, appids) {
	log("STATUS", "VAC ban" + (numBans == 1 ? '' : 's') + ": " + numBans)
	if (appids.length > 0) {
		log("STATUS", "VAC banned from apps: " + appids.join(', '))
	}
})

client.on('licenses', function(licenses) {
	log("STATUS", "Account owns " + licenses.length + " license" + (licenses.length == 1 ? '' : 's') + ".")
})

client.on('friendRelationship', function(sid, relationship) {
	log("STATUS", "Relationship of " + sid + " changed to " + SteamUser.EFriendRelationship[relationship])

	if (relationship === SteamUser.EFriendRelationship.RequestRecipient) {
		client.addFriend(sid, function(err, personaName) {
			if (err)
				log("WARN", "Error adding friend: " + err)
			else
				log("INFO", "Successfully added friend: " + personaName)
		})
	}
})

client.on('friendsList', function() {
	log("STATUS", "Friend list: " + Object.keys(client.myFriends).map(sid => sid + " (" + SteamUser.EFriendRelationship[client.myFriends[sid]] + ")").join(", "))
})

csgo.on('connectedToGC', function() {
	log("STATUS", "Connected to GC")

	loadFriendProfiles()
	setTimeout(() => { loadFriendProfiles() }, config.refreshInterval)
})

csgo.on('disconnectedFromGC', function(reason) {
	log("STATUS", "Disconnected from GC, reason: " + reason)
})

csgo.on('connectionStatus', function(status, data) {
	log("STATUS", "GC status changed to " + status + " (" + data + ")")
})

csgo.on('playersProfile', (profile) => {
	const sid = SteamID.fromIndividualAccountID(profile.account_id).getSteamID64()
	log("INFO", "User " + sid + " is level " + profile.player_level + " and has rank " + profile.ranking.rank_id + ", profile data:")
	if (process.argv.includes("-v"))
		console.log(profile)
	friendProfiles[sid] = profile.ranking.rank_id
	log("INFO", "New friend profile data:")
	console.log(friendProfiles)
})

module.exports = {
	start: () => {
		log("INFO", "Logging in")

		client.logOn(config.account)
	},

	ranks: friendProfiles
}
