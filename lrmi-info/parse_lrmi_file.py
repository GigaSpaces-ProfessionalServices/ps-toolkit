#!/usr/bin/python

import sys

file_name = sys.argv[1]

def process_lrmi_line(text):
    clean = text.strip()
    if text[0] == ',':
        clean = text[1:len(text)]
    last_char = clean[len(clean) - 1:len(clean)]
    if last_char == ',':
        clean = clean[0:len(clean) - 1]
    if clean[len(clean) - 1:len(clean)] == ',':
        dummy = clean
        clean = process_lrmi_line(dummy)
    if isinstance(clean, str):
        x = clean.split(',')
    else:
        x = clean
    return x


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
    parse_mode = 0

    current_space_uid = None
    current_processed_time = None

    uid_to_space_map = dict()
    uid_lrmi_info_map = dict()

    for line in file(file_name):

        some_uid = None
        some_space_name = None

        count += 1

        if count == limit and do_limit:
            break

        # Look for GSC uid
        if line.find('GSC:') != -1:

            begin = line.index('[') + 1
            uid = line[begin:len(line)].rstrip()

            num_uids = len(gsc_uids)
            gsc_uids.add(uid)
            if len(gsc_uids) > num_uids:
                current_space_uid = uid
                print 'Detected new GSC {0}'.format(uid)
            parse_mode = 1

        else:

            # deal with property followed by 'XXX BUNCH OF TEXT'
            if parse_mode == 1:
                x_loc = line.find('XXX')
                if x_loc > 0:
                    head = line[0:x_loc]
                    temp = process_lrmi_line(head)
                    uid_lrmi_info_map[current_space_uid] = [temp]
                    parse_mode = 2
                    # print '{0} {1}'.format(temp[0], temp[1])
                else:
                    if line.find('~~~') == -1:
                        tup = process_lrmi_line(line)
                        x = uid_lrmi_info_map[current_space_uid]
                        if x:
                            uid_lrmi_info_map.update(current_space_uid, tup)
                        else:
                            uid_lrmi_info_map[current_space_uid, [tup]]
                        # print '{0} {1}'.format(tup[0], tup[1])

            # sniff for space name until we find it
            if parse_mode == 2:
                space_name = look_for_space_name(line)
                if space_name:
                    if current_space_uid not in gsc_uids:
                        uid_to_space_map[current_space_uid] = space_name
                        current_space_uid = space_name
                        # print 'Detected New SpaceName: {0}'.format(space_name)
                    parse_mode = 3

            # process properties
            if parse_mode == 3:
                if line.find('~~~') != -1:
                    tup = process_lrmi_line(line)
                    print '{0} {1}'.format(tup[0], tup[1])
                    uid_lrmi_info_map[current_space_uid] = [tup]
                else:
                    parse_mode = 0

        # end for loop

        for tup in uid_to_space_map:
            uid = tup[0]
            print "SpaceName,{0},GSC,{1}".format(tup[0], tup[1])
            for infoList in uid_lrmi_info_map[uid]:
                first_one = True
                counter = 0
                num_metrics = len(infoList)
                for pair in infoList:
                    if not first_one:
                        print "{0},{1}".format(pair[1], pair[2])
                    else:
                        if counter != num_metrics:
                            print ",{0}"
                        else:
                            print ",{0}\n"
                    counter += 1

    print 'Detected {0} GSCs'.format(len(gsc_uids))