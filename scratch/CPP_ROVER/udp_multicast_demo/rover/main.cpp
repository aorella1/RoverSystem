#include <stdlib.h>
#include <stdio.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>

#include <string>

#define CONNECTION_DISCOVERY_ADDRESS "233.252.66.85"
#define CONNECTION_DISCOVERY_PORT 44444

int main() {
    int socket_fd = socket(AF_INET, SOCK_DGRAM, 0);
    if (socket_fd < 0)
    {
        // Socket open failure
        printf("[!] Failed to open socket!\n");
        return 1;
    }

    std::string message("Hey there!");

    while (1) {
        struct sockaddr_in send_addr;
        memset((char*)&send_addr, 0, sizeof(send_addr));
        send_addr.sin_family = AF_INET;
        send_addr.sin_port = htons(CONNECTION_DISCOVERY_PORT);
        inet_aton(CONNECTION_DISCOVERY_ADDRESS, &send_addr.sin_addr);
    
        if (sendto(socket_fd, message.c_str(), message.length(), 0, (struct sockaddr*) &send_addr, sizeof(send_addr)) < 0)
        {
            // Send failure
            printf("[!] Failed to send packet!\n");
            return 1;
        }

        printf("> Sent.\n");

        usleep(999 * 1000);
    }



    return 0;
}