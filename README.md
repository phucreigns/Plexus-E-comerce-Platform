*Plexus-E-comerce-Platform*

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



Demo: Registration
<img width="1470" height="822" alt="Screenshot 2025-11-30 at 21 48 14" src="https://github.com/user-attachments/assets/5738d3d4-1751-497c-be6b-5d455677406d" />

<img width="1470" height="814" alt="Screenshot 2025-11-30 at 21 48 51" src="https://github.com/user-attachments/assets/b4c44dce-b71a-4810-86e9-8f9d54906b3e" />

<img width="1470" height="789" alt="Screenshot 2025-11-30 at 21 49 35" src="https://github.com/user-attachments/assets/d2bbeb88-dbca-4c0c-98f3-50621f65ca54" />

Demo: Email Authentication 
<img width="1470" height="816" alt="Screenshot 2025-11-30 at 21 50 21" src="https://github.com/user-attachments/assets/2ed672dd-c339-4901-921f-50b44fbd76aa" />

Demo: Login (Email & Password)
<img width="1470" height="781" alt="Screenshot 2025-11-30 at 22 04 25" src="https://github.com/user-attachments/assets/674c592d-15d0-46b2-9806-91a6a9a9cb37" />

<img width="1203" height="738" alt="Screenshot 2025-11-30 at 21 51 43" src="https://github.com/user-attachments/assets/64fb44e3-03ad-45b6-8423-2e23c57c7b79" />


#Databases Management

PostgresSQL






























