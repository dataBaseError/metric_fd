require 'pg'
require 'date'

class DBInterface

    USERNAME = "postgres"
    PASSWORD = "z3vvt0aTlkoPb5"


    def initialize(name)
        @conn = PG.connect(dbname: name, user: USERNAME, password: PASSWORD, hostaddr: "127.0.0.1")
    end

    def insert(table, attributes)

        prev_null = 0
        attribute_list = Array.new
        attributes.each_with_index do |val, i|

            if val == ""
                val = "null"
            else
                if i == 2 || i == 3 || i == 5 || i == 6
                    # In case there is just random data we will set it to null
                    begin
                        val = DateTime.parse(val).to_time.to_i.to_s
                    rescue ArgumentError
                        val = "null"
                    end

                    if val == "null"
                        if i == 2 || i == 5
                            prev_null = 2
                        elsif (i == 3 || i == 6) && prev_null <= 0
                            val = attribute_list[-1]
                        end
                    elsif (i == 3 || i == 6) && prev_null > 0
                        attribute_list[-1] = val
                    end

                else
                    val = "'#{val}'"
                end
            end
            if prev_null > 0
                prev_null -= 1
            end
            attribute_list << val
        end

        puts "attribute_list = #{attribute_list}"
        @conn.exec("INSERT INTO #{table} (provider, flight_number, schedualed_departure, actual_departure, departure_id, schedualed_arrival, actual_arrival, arrival_id) VALUES (#{attribute_list.join(", ")})") do |results|

        end
    end
end

file = ""

if ARGV.size == 1
    file = ARGV[0]
else 
    Kernel.exit(true)
end

table = "clean_flight"

db = DBInterface.new(table)

IO.foreach(file) do |line|

    cleaned_line = line.chars.select(&:valid_encoding?).join
    puts "line = #{line.chomp}"
    puts "line = #{line.chars}"
    result = cleaned_line.split(/\t/)
    result[-1].chomp!
    puts "line split = #{result}"
    #a = gets
    db.insert(table, result)
end