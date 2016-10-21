#!/usr/bin/python

import sys

file_name = sys.argv[1]

headers = ['current time', 'machine', 'PID', 'started', 'all threads count']

def process_lrmi_line(text):
    clean = text.strip()
    if text[0] == ',':
        clean = text[1:len(text)]
    last_char = clean[len(clean) - 1:len(clean)]
    if last_char == ',' or last_char.isspace():
        clean = clean[0:len(clean) - 1]
    if isinstance(clean, str) and ',' in clean:
        return clean.split(',', 1)
    return clean

def process_header_line(text):
    clean = text.strip()
    header = next(header for header in headers if header in clean)
    return (header, clean[len('~~~ ') + len(header) + 1:len(clean)])

def look_for_space_name(text):
    c_loc = text.find('SpaceImpl')
    if c_loc != -1:
        open_paren_loc = c_loc + len('SpaceImpl')
        close_paren_loc = text.find(')', open_paren_loc)
        name = text[open_paren_loc + 1:close_paren_loc]
        return name
    return None


if __name__ == '__main__':

    gsc_uids = set()
    count = 100
    limit = 1200
    do_limit = False

    current_space_uid = None
    current_processed_time = None

    uid_to_space_map = dict()
    uid_lrmi_info_map = dict()

    skip = False
    
    for line in file(file_name):

        some_uid = None
        some_space_name = None

        count += 1

        if count == limit and do_limit:
            break

        # Look for GSC uid
        if line.find('~~~ GSC') != -1:
            uid = line.split()[2]

            gsc_uids.add(uid)
            current_space_uid = uid
            
            skip = False
            #print 'Detected new GSC {0}'.format(uid)
            continue
        # deal with property followed by 'XXX BUNCH OF TEXT'
        if line.find('XXX') >= 0:
            skip = True
            space_name = look_for_space_name(line)
            if space_name:
                uid_to_space_map[current_space_uid] = space_name
                continue
        else:
            if line.find('~~~') != -1:
                tup = process_header_line(line)
            else:
                tup = process_lrmi_line(line)
        if skip: 
            continue
        if  current_space_uid in uid_lrmi_info_map:
            if tup[0] in uid_lrmi_info_map[current_space_uid]:
                uid_lrmi_info_map[current_space_uid][tup[0]].append(tup[1])
            else:
                uid_lrmi_info_map[current_space_uid][tup[0]] = [tup[1]]
        else:
            uid_lrmi_info_map[current_space_uid] = {tup[0]:[tup[1]]}
            
    # end for loop
    for uid, space_name in uid_to_space_map.iteritems():
        print "SpaceName,{0},GSC,{1}".format(space_name, uid)
        counter = 0
        num_metrics = len(uid_lrmi_info_map[uid])
        for stat, values in uid_lrmi_info_map[uid].iteritems():
            if counter != num_metrics:
                print ",{0},{1}".format(stat, ','.join(values))
            else:
                print ",{0},{1}\n".format(stat, ','.join(values))
            counter += 1

    print 'Detected {0} GSCs'.format(len(gsc_uids))
