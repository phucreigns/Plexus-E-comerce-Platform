🖥️ The E-Commerce Backend system offers APIs covering product management, carts, orders, users, payments, and file uploads. It is built with a focus on scalability, JWT security, caching, and asynchronous processing for intensive tasks.


<img width="1190" height="650" alt="image" src="https://github.com/user-attachments/assets/344d6d7a-dd74-46be-a9a2-df77fe78b154" />


# Architecture Overview

✨ Microservices Architecture.
- Service Granularity: Services are small and perform a single function.
- Decentralized Data Management: Each service typically manages its own database to ensure independence. This is often referred to as "Database per Service."
- Independent Deployment: Services can be developed, deployed, and updated independently without affecting the rest of the application.
- Technology Heterogeneity: Different services can be written in different use different storage technologies, choosing the best tool for the job.
- Inter-Service Communication: Services communicate with each other, usually through lightweight mechanisms like HTTP/REST APIs.
- Resilience: If one service fails, the others can continue to function, making the overall system more fault-tolerant.
<img width="1000" height="453" alt="image" src="https://github.com/user-attachments/assets/15f5fb62-5918-4894-aae7-1b26fbb6249b" />




