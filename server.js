var net = require("net");
var sys = require("sys");
var strftime = require("date_format").strftime;

// Konstante
var welcomeMsg = "Hello!";
var dateFmt = "%Y-%m-%d %R:%S";


var sada = function(){
	return strftime(new Date(), dateFmt);
}


Array.prototype.remove = function(e) {
	for (var i = 0; i < this.length; i++) {
		if (e == this[i]) { return this.splice(i, 1); }
	}
};

function Client(stream) {
	this.name = null;
	this.stream = stream;
}

var clients = [];

var server = net.createServer(function (stream) {
	var client = new Client(stream);
	clients.push(client);

	stream.setTimeout(0);
	stream.setEncoding("utf8");

	stream.addListener("connect", function () {
		// stream.write("Tvoj nickname:\n");
	});

	stream.addListener("data", function (data) {
		if (client.name == null) {
			client.name = data.match(/\S+/);
			// clients.forEach(function(c) {
			// 	if (c.name == client.name) {
			// 		stream.write("Error: Nick je zauzet!\n")
			// 		stream.end();
			// 	}
			// });

			stream.write(welcomeMsg + "\n");
			var cnt=0;
			clients.forEach(function(c) { cnt+=1; });
			clients.forEach(function(c) {
				if (c != client) {
					c.stream.write(sada() + ": " + "<"+client.name+">" + " je dosao.\n");
				} else {
					c.stream.write("Tenutno je spojeno " + cnt + " korisnika.\n");
				}
			});
			sys.puts(sada() + ": " + "<"+client.name+">" + " je dosao.");
			return;
		}

		var command = data.match(/^\/(.*)/);
		if (command) {
			if (command[1] == 'users' || command[1] == 'korisnici') {
				clients.forEach(function(c) {
					stream.write("- " + c.name + "\n");
				});
			}
			else if (command[1] == 'quit') {
				stream.end();
			}
			else if (command[1] == 'userscount') {
				var cnt=0;
				clients.forEach(function(c) { cnt+=1; });
				stream.write("#N "+cnt+"\n");
			}
			return;
		}

		clients.forEach(function(c) {
			if (c != client) {
				c.stream.write("<"+client.name+"> " + data);
			}
		});
		sys.print(sada() + ": " + "<"+client.name+"> " + data);
	});

	stream.addListener("end", function() {
		clients.remove(client);

		clients.forEach(function(c) {
			c.stream.write(sada() + ": " + "<"+client.name+">" + " je otisao.\n");
		});

		sys.puts(sada() + ": " + "<"+client.name+">" + " je otisao.");

		stream.end();
	});
});

server.listen(7000);
