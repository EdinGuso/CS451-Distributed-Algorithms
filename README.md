# CS453-Distributed Algorithms

This repo includes the project I worked on for the CS451: Distributed Algorithms course at EPFL during Fall 2022 semester.

## Project Summary:

The goal of this practical project is to implement certain building blocks necessary for a decentralized system. To this end, some underlying abstractions will be used:

  - Perfect Links ([Step #1](https://github.com/EdinGuso/CS451-Distributed-Algorithms/tree/main/step1)),
  - Best-effort Broadcast
  - Uniform Reliable Broadcast
  - FIFO Broadcast ([Step #2](https://github.com/EdinGuso/CS451-Distributed-Algorithms/tree/main/step2))
  - Lattice Agreement ([Step #3](https://github.com/EdinGuso/CS451-Distributed-Algorithms/tree/main/step3))

Various applications (e.g., a payment system) can be built upon these lower-level abstractions.

The project was graded based on correctness (4/6) and performance among other students (2/6). My implementation received a 6/6 grade on all steps by achieving full correctness and best performance among Java submissions.

## Running the Project:

In order to run the program: clone this repository, move into the submission step folder you wish to test and run the following command:

* For step1:
  * `you@your-pc:/path_to_repository/step1$ ./build.sh`
  * `you@your-pc:/path_to_repository/step1$ ../tools/stress.py perfect -r ./run.sh -l ../output -p PROCESSES -m MESSAGES`
  * where `PROCESSES` is number of processes to start and `MESSAGES` is number of messages to send.

* For step2:
  * `you@your-pc:/path_to_repository/step2$ ./build.sh`
  * `you@your-pc:/path_to_repository/step2$ ../tools/stress.py fifo -r ./run.sh -l ../output -p PROCESSES -m MESSAGES`
  * where `PROCESSES` is number of processes to start and `MESSAGES` is number of messages to send.

* For step3:
  * `you@your-pc:/path_to_repository/step3$ ./build.sh`
  * `you@your-pc:/path_to_repository/step3$ ../tools/stress.py agreement -r ./run.sh -l ../output -p PROCESSES -n PROPOSALS -v PROPOSAL_MAX_VALUES -d PROPOSALS_DISTINCT_VALUES`
  * where `PROCESSES` is number of processes to start (at most 128), `PROPOSALS` is number of proposals to perform, `PROPOSAL_MAX_VALUES` is the maximum number of elements a process can propose in a single round, `PROPOSAL_DISTINCT_VALUES` is the maximum number of unique elements that can be proposed by all processes in a single round.

All output files will be in [`/path_to_repository/output`](https://github.com/EdinGuso/CS451-Distributed-Algorithms/tree/main/output)

**Compilation Environment:**
* Ubuntu 18.04 running on a 64-bit architecture
* OpenJDK Runtime Environment (build 11.0.8+10-post-Ubuntu-0ubuntu118.04.1)
* Apache Maven 3.6.3