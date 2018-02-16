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

    struct sockaddr_in address;
    memset((char*)&address, 0, sizeof(address));
    address.sin_family = AF_INET;
    inet_aton("0.0.0.0", &address.sin_addr);
    address.sin_port = htons(CONNECTION_DISCOVERY_PORT);

    if (bind(socket_fd, (struct sockaddr*) &address, sizeof(address)) < 0)
    {
        // Bind failure
        printf("[!] Failed to bind socket!\n");
        return 1;
    }

    uint8_t buffer[100];
    struct sockaddr src_addr;
    socklen_t src_addr_len;

    ssize_t res;

    // Request addition to a multicast group.
    struct ip_mreq mreq;
    mreq.imr_multiaddr.s_addr=inet_addr(CONNECTION_DISCOVERY_ADDRESS);
    mreq.imr_interface.s_addr=htonl(INADDR_ANY);
    if (setsockopt(socket_fd,IPPROTO_IP,IP_ADD_MEMBERSHIP,&mreq,sizeof(mreq)) < 0) {
        printf("[!] Failed to request multicast listening!\n");
        return 1;
    }

    while (1)
    {
        src_addr_len = sizeof(src_addr);

        res = recvfrom(socket_fd, buffer, 100, MSG_DONTWAIT, &src_addr, &src_addr_len);
        if (res == -1)
        {
            // Two options here: either its because no packets were around, or there's an actual error...
            if (errno == EAGAIN)
            {
                printf("> Nothing to read.\n");
                usleep(1000 * 1000);
                continue;
            }
            else
            {
                // Handle failure
                printf("[!] Failed to receive packet!\n");
                return 1;
            }
        }

        // Get sender information
        struct sockaddr_in src_addr_in = *((struct sockaddr_in*) &src_addr);
        int port = (int) ntohs(src_addr_in.sin_port);
        std::string address(inet_ntoa(src_addr_in.sin_addr));

        printf("Got message from %s:%d\n", address.c_str(), port);

        printf("\t\"%s\"\n", buffer);
    }

    return 0;
}