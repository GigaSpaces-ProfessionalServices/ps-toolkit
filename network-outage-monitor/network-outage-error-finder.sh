#!/bin/bash
set -o nounset

# The list of log files name regexes delimited by ';'
readonly LOG_FILE_REGEXES='^.*/[0-9]{4}-[0-9]{2}-[0-9]{2}~[0-9]{2}.[0-9]{2}-gigaspaces-[a-z]{3}.*\.log;^.*/[a-z]{3}.*\.log'
# The base dir with logs
readonly BASE_LOG_DIR=/gigaspaces/scripts/logs/
readonly OUTPUT_FILE_NAME=/dev/stderr

# Split LOG_FILE_REGEXES into the array
readonly REGEXES_ARR=(${LOG_FILE_REGEXES//;/ })
# The empty array for matched log files
FOUND_FILES_ARRAY=()

# Search log files by regexes and put them to FOUND_FILES_ARRAY
for element in "${REGEXES_ARR[@]}"; do
    for file in "$(find ${BASE_LOG_DIR} -type f -regextype posix-extended -regex ${element})"; do
        FOUND_FILES_ARRAY+=(${file})
    done
done

# Remove duplicates from FOUND_FILES_ARRAY
readonly UNIQUE_FILES=($(for file in "${FOUND_FILES_ARRAY[@]}"; do echo "${file}" ; done | sort -du))

# Go through found logs and list errors
echo -e "Errors found:" | tee ${OUTPUT_FILE_NAME}
for file in "${UNIQUE_FILES[@]}"; do
    error=$(grep -rnw ${file} -e "Replication channel" -e "Failed to connect to LUS")

    if [ ! -z "${error}" ]; then
        echo "${file}:" | tee ${OUTPUT_FILE_NAME}
        echo -e "${error}" | tee ${OUTPUT_FILE_NAME}

        recovery=$(grep -rnw ${file} -e "Recovered")
        if [ ! -z "${recovery}" ]; then
            echo -e "\nRecovery messages:\n${recovery}" | tee ${OUTPUT_FILE_NAME}
        fi
        echo -e "\n" | tee ${OUTPUT_FILE_NAME}
    fi
done