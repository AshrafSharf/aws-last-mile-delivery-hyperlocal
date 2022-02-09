/*********************************************************************************************************************
 *  Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.                                               *
 *                                                                                                                   *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of                                  *
 *  this software and associated documentation files (the "Software"), to deal in                                    *
 *  the Software without restriction, including without limitation the rights to                                     *
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of                                 *
 *  the Software, and to permit persons to whom the Software is furnished to do so.                                  *
 *                                                                                                                   *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR                                       *
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS                                 *
 *  FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR                                   *
 *  COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER                                   *
 *  IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN                                          *
 *  CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                                       *
 *********************************************************************************************************************/
const axios = require('axios').default
const logger = require('../utils/logger')
const config = require('../config')

const execute = async (payload) => {
	logger.info('Executing invoke dispatch for payload')
	logger.info(payload)
	const { centroid, orders, executionId } = payload
	const url = `http://${config.dispatchEngineElbDNS}/instant/sequential/dispatch/assign-drivers`

	try {
		const results = await axios.post(url, JSON.stringify({
			centroid,
			orders,
			executionId,
		}), {
			headers: {
				'Content-Type': 'application/json',
			},
		})

		logger.debug('Results from dispatch engine:')
		logger.debug(results.data)
		const { data: { problemId } } = results

		return {
			problemId,
		}
	} catch (err) {
		logger.error('Error on dispatch request')
		logger.error(err)

		throw Error('Error invoking the dispacher')
	}
}

module.exports = {
	execute,
}
